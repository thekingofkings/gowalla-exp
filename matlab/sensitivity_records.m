
files = ls('../data_sensitivity/distance-d30-u5000-*');
    
figure();
hold on;
box on;
grid on;
    
colors = [ [0, 0, 0.8]; [0.3, 0.6, 0.9]; ...
    [0, 100, 0] / 255; [0, 0.75, 0]; ...
    [255, 140, 0]/ 255; [255, 215, 0] / 255;  ...
    [153,50,204]/255; [216,191,216] /255; ...
    [143,188,143]/255; [0,128,128]/255];
color_ind = 1;
g = zeros(1, size(files,1));
auc = zeros(size(files, 1)+1, 3);
for ind = 1:size(files,1)
    
    data = importdata(['../data_sensitivity/', files(ind,:)]);
    [~, i] = sort(data(:,6));
    data = data(i, :);  

    size(data)

    pbg_locen = data(:,3);
    locen = data(:,4);
    pbg = data(:,5);
    freq = data(:,6);
    pbg_locen_td = data(:,7);
    td = data(:,8);
    friflag = data(:,9);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    

    l = zeros(1,3);
    [pre, rec, l(1)] = precisionRecallPlot( freq, friflag, 'linestyle', '-', 'color', colors(color_ind,:) );
    auc(ind+1,1) = trapz(rec, pre);
%     precisionRecallPlot( pbg, friflag, 'r--' );
%     precisionRecallPlot( locen, friflag, 'linestyle', '--', 'color', [0, 0.75, 0] );
%     precisionRecallPlot( td, friflag, 'linestyle', '--', 'color', [255, 215, 0] / 255 );
    
    [pre, rec, l(2)] = precisionRecallPlot( pbg_locen, friflag, 'linestyle', ':', 'color', colors(color_ind,:) );
    auc(ind+1,2) = trapz(rec, pre);
    [pre, rec, l(3)] = precisionRecallPlot( pbg_locen_td, friflag, 'linestyle', '--', 'color', colors(color_ind,:) );
    auc(ind+1,3) = trapz(rec, pre);
    color_ind = color_ind + 1;

    g(ind) = hggroup;
    set(l(1:3), 'Parent', g(ind));
end

%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend([g], {'Freq/PBG/PGT 20%', 'Freq/PBG/PGT 50%', ...
        'Freq/PBG/PGT 80%', 'Freq/PBG/PGT 100%'}, 'location', 'northeast');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print('sensitivity-recs.eps', '-dpsc');
    system('epstopdf sensitivity-recs.eps');
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);


pbg_base = auc(:,2) - auc(:,1);
overall_base = auc(:,3) - auc(:,1);
overall_pbg = auc(:,3) - auc(:,2);
x = 0:51.2:512.75;
x = round(x);

figure;
hold on;
grid on;
box on;
plot(x, pbg_base, '-', 'color', [200, 0, 0] / 255, 'linewidth', 3);
plot(x, overall_base, '--', 'color', [0.3, 0.6, 0.9], 'linewidth', 3);
plot(x, overall_pbg, '-.', 'color', [0, 100, 0] / 255, 'linewidth', 3);
set(gca, 'linewidth', 2, 'fontsize', 18);
xlabel('Average #check-ins', 'fontsize', 20);
ylabel('AUC', 'fontsize', 20);
legend({'Personal - Frequency', 'Overall - Frequency', 'Overall - Personal'}, 'location', 'northwest');
print('sensitivity-recs.eps', '-dpsc');
system('epstopdf sensitivity-recs.eps');
