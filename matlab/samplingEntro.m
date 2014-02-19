files = ls('../distance-d30-u5000c1.50000-*s.txt');

figure();
hold on;
box on;
grid on;
styles = {'-.', '--', '-'};

for i = 1:size(files,1)
    dml5 = importdata(['../', files(i,:)]);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);


    dml5 = dml5(dml5(:,6) > 1,:);
    sum(dml5(:,7)==1)
    sum(dml5(:,7)==0)
    size(dml5);

    prod_colcEnt_cm = dml5(:,3);
    locwf5 = dml5(:,4);
    locm5 = dml5(:,5);
    locf5 = dml5(:,6);
    dl5 = dml5(:,7);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    

    precisionRecallPlot( locwf5, dl5, 'linestyle', styles{i}, 'color', [0+i * 0.3, 0.3, 0.3] );

%     precisionRecallPlot( prod_colcEnt_cm, dl5, 'k--' );
 


%     title(num2str(condition));
    
%     axis([0,1,0.5,1]);
end
    
%     precisionRecallPlot( locm5, dl5, 'r--' );
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    precisionRecallPlot( locf5, dl5, 'linestyle', ':', 'color', 'blue', 'linewidth', 5);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'5000 users', '500 users','100 users', 'Frequency'}, 'location', 'best');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print(['pr-tn.eps'], '-dpsc');
    system(['epstopdf pr-tn.eps']);
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);