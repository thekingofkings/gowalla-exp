tau = 1;

if tau == 1
    files = ls('../data_sensit_user/sle-*.txt');
elseif tau == 4
    files = ls('../data_sensit_user/distance-d30-leu*-c0.200.txt');
end

% sort the files by the number of users
numUser = zeros(size(files,1), 1);
for i = 1:size(files,1)
    tmp = textscan(files(i,:), 'sle-u%f.txt');
    numUser(i) = tmp{1};
end
[~, ind] = sort(numUser, 'descend');
files = files(ind, :);

figure();
hold on;
box on;
grid on;
styles = {'-.', '--', '-'};
colors = {[220,20,60]/255, [144,238,144]/ 255, [0,0,139]/255};

for i = 1:size(files,1)
    dml5 = importdata(['../data_sensit_user/', files(i,:)]);
    dml5 = dml5(dml5(:,6) > 0,:);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);

    prod_colcEnt_cm = dml5(:,3);
    locen = dml5(:,4);
    pbg = dml5(:,5);
    freq = dml5(:,6);
    pbg_locen_td = dml5(:,7);
    td = dml5(:,8);
    frilabel = dml5(:,9);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    


    precisionRecallPlot( locen, frilabel, 'linestyle', styles{i}, 'color', colors{i} );
 
end
   
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    precisionRecallPlot( freq, frilabel, 'linestyle', ':', 'color', [0,100,0]/255, 'linewidth', 5);

    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 3, 'fontsize', 20);
    legend({'5000 users', '500 users','100 users', 'Frequency'}, 'location', 'northeast');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print('sampleEntro.eps', '-dpsc');
    system('epstopdf sampleEntro.eps');
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);